import PropTypes from 'prop-types';
// material

import { Card, TableRow, TableCell, TableHead, TableContainer, Table, TableBody } from '@mui/material';


// ----------------------------------------------------------------------

AEList.propTypes = {
  graphNodes: PropTypes.array.isRequired
};

export default function AEList({ graphNodes, filterHandler}) {
    let AEs = graphNodes.filter( (graphNode) => {
        return graphNode.classes?.includes("adverse event");
    });
    return (
        <Card>
            <TableContainer>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>
                                ID
                            </TableCell>
                            <TableCell>
                                Weight
                            </TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {AEs.sort((a,b) => b.data.llr - a.data.llr).map( (AE) => (
                            <TableRow key={AE.data.id} onClick={(e) => filterHandler(AE.data.id)}>
                                <TableCell>
                                    {AE.data.name}
                                </TableCell>
                                <TableCell>
                                    {AE.data.llr}
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table >
            </TableContainer>
        </Card>
    )
}
